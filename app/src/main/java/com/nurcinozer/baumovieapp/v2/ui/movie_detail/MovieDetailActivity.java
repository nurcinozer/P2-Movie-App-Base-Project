package com.nurcinozer.baumovieapp.v2.ui.movie_detail;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.classification.MLImageClassification;
import com.huawei.hms.mlsdk.classification.MLImageClassificationAnalyzer;
import com.huawei.hms.mlsdk.classification.MLLocalClassificationAnalyzerSetting;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.nurcinozer.baumovieapp.v2.R;
import com.nurcinozer.baumovieapp.v2.data.movie_detail.Genre;
import com.nurcinozer.baumovieapp.v2.data.movie_detail.MovieDetailModel;
import com.nurcinozer.baumovieapp.v2.databinding.ActivityMovieDetailBinding;
import com.nurcinozer.baumovieapp.v2.util.Constants;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MovieDetailActivity extends AppCompatActivity {

    private ActivityMovieDetailBinding binding;
    private MovieDetailViewModel mViewModel;
    private Bitmap posterBitmap;

    private MLImageClassificationAnalyzer analyzer;
    private final String TAG = "ImageDetectionFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMovieDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Prepare View Model
        mViewModel = ViewModelProviders.of(this).get(MovieDetailViewModel.class);

        checkArguments();
        initObservers();
    }

    /**
     * Check arguments via SafeArg
     */
    private void checkArguments() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            int movieId = bundle.getInt(Constants.ARG_MOVIE_ID);
            mViewModel.getMovieDetail(movieId);
        } else finish();
    }

    /**
     * Initialize observers for getting data from the ViewModel
     */
    private void initObservers() {
        mViewModel.getMovieDetail().observe(this, this::prepareComponents);
    }

    /**
     * Set the data to view components
     *
     * @param movie as MovieDetailModel
     */
    private void prepareComponents(MovieDetailModel movie) {
        //Backdrop image
        if (!TextUtils.isEmpty(movie.getBackdropPath())) {
            String posterPath = Constants.BACKDROP_BASE_PATH + movie.getBackdropPath();

            // Additional Reference for handling background tasks: https://stackoverflow.com/questions/58767733/android-asynctask-api-deprecating-in-android-11-what-are-the-alternatives
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                //Background work here
                try {
                    posterBitmap = Glide.with(getApplicationContext())
                            .asBitmap()
                            .load(posterPath)
                            .placeholder(R.drawable.ic_baseline_broken_image_24)
                            .submit().get();


                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                handler.post(() -> {
                    //UI Thread work here
                    binding.ivBackdrop.setImageBitmap(posterBitmap);

                    if (posterBitmap == null) {
                        Toast.makeText(this, "Image could not be found", Toast.LENGTH_SHORT).show();
                    }

                    Button imageClassificationButton = findViewById(R.id.image_classification_btn);
                    TextView detectedClassesTv = findViewById(R.id.detected_image_class_tv);

                    // Create the image analyzer:
                    analyzer = createImageAnalyzer();

                    // TODO: Create an MLFrame object using the bitmap that is the image data in bitmap format - YAY DONE!!
                    MLFrame frame = MLFrame.fromBitmap(posterBitmap);
                    imageClassificationButton.setOnClickListener(view1 -> {
                        Task<List<MLImageClassification>> task = analyzer.asyncAnalyseFrame(frame);
                        task.addOnSuccessListener(classifications -> {
                            // Recognition success.
                            StringBuilder sb = new StringBuilder();
                            sb.append("Results: \n\n");
                            // All classification results will be added to StringBuilder object in a for loop:
                            for (int i = 0; i < classifications.size(); i++) {
                                sb.append("[")
                                        .append(i)
                                        .append("] ")
                                        .append(classifications.get(i).getName())
                                        .append("\n");

                            }
                            if (classifications.size() > 0) {
                                detectedClassesTv.setText(sb.toString());
                            } else {
                                detectedClassesTv.setText("Results: \n\n" + "[0] Others");
                            }
                            // After recognition is completed, release the detection resources:
                            releaseImageDetectionResources();

                        }).addOnFailureListener(e -> {
                            // Recognition failure.
                            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();;
                        });
                    });
                });
            });
        }
        //Overview
        if (!TextUtils.isEmpty(movie.getOverview()))
            binding.tvOverview.setText(movie.getOverview());
        //Genres / Categories
        if (movie.getGenres() != null & movie.getGenres().size() > 0) {
            String genres = "";
            for (Genre genre : movie.getGenres()) {
                if (genres.equals(""))
                    genres = genre.getName();
                else genres += ", " + genre.getName();
            }
            binding.tvCategory.setText(genres);
        }
        //Release Date
        if (!TextUtils.isEmpty(movie.getReleaseDate()))
            binding.tvDate.setText(movie.getReleaseDate());
        //Score
        if (movie.getVoteAverage() != 0)
            binding.tvScore.setText(movie.getVoteAverage().toString());
    }

    private MLImageClassificationAnalyzer createImageAnalyzer() {
        MLLocalClassificationAnalyzerSetting setting =
                new MLLocalClassificationAnalyzerSetting.Factory()
                        .setMinAcceptablePossibility(0.8f) // Higher min acceptable possibility results in higher accuracy in classification results.
                        .create();
        return MLAnalyzerFactory.getInstance().getLocalImageClassificationAnalyzer(setting);
    }

    private void releaseImageDetectionResources() {
        try {
            if (analyzer != null) {
                analyzer.stop();
            }
        } catch (IOException e) {
            // Exception handling.
            Log.e(TAG, e.getLocalizedMessage());
        }
    }
}