package com.nurcinozer.baumovieapp.v2.ui.movie_search;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;
import com.nurcinozer.baumovieapp.v2.adapter.MovieAdapter;
import com.nurcinozer.baumovieapp.v2.data.movie_search.Result;
import com.nurcinozer.baumovieapp.v2.databinding.ActivityMovieSearchBinding;
import com.nurcinozer.baumovieapp.v2.ui.movie_detail.MovieDetailActivity;

import java.util.List;

import static com.nurcinozer.baumovieapp.v2.util.Constants.ARG_MOVIE_ID;

public class MovieSearchActivity extends AppCompatActivity {

    private ActivityMovieSearchBinding binding;
    private MovieSearchViewModel mViewModel;
    private MovieAdapter movieAdapter;
    public HuaweiIdAuthService service;
    private static final int REQUEST_SIGN_IN_LOGIN = 3001; // Normal Login

    @Override
    protected void onStart() {
        HuaweiIdAuthParams authParams = new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM).createParams();
        service = HuaweiIdAuthManager.getService(MovieSearchActivity.this, authParams);
        Task<AuthHuaweiId> task = service.silentSignIn();
        task.addOnSuccessListener(new OnSuccessListener<AuthHuaweiId>() {
            @Override
            public void onSuccess(AuthHuaweiId userAccount) {
                Toast.makeText(MovieSearchActivity.this, "Hello " + userAccount.getDisplayName(), Toast.LENGTH_SHORT).show();
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // The sign-in failed. Try to sign in explicitly using getSignInIntent().
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException)e;
                    Log.i("SIGN-IN-FAIL", "sign failed status:" + apiException.getStatusCode());

                    Intent signInIntent = service.getSignInIntent();
                    MovieSearchActivity.this.startActivityForResult(signInIntent, REQUEST_SIGN_IN_LOGIN);

                    //goToLoginActivity(); //try this if not directed to LoginActivity upon failure.
                    //This is a method I wrote to start an intent. Nothing special. ;)
                }
            }
        });

        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMovieSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Prepare View Model
        mViewModel = ViewModelProviders.of(this).get(MovieSearchViewModel.class);

        showWelcomeMessage();
        initComponents();
        initClicks();
        initObservers();

    }

    /**
     * Show the Toast message to user
     */
    private void showWelcomeMessage() {
        Toast.makeText(this, "Welcome to MyApp", Toast.LENGTH_SHORT).show();
    }

    /**
     * Initialize components & Create necessary adapter
     */
    private void initComponents() {
        //Recycler View initialize
        binding.rvMovies.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMovies.setItemAnimator(new DefaultItemAnimator());
        movieAdapter = new MovieAdapter(this);
        binding.rvMovies.setAdapter(movieAdapter);
    }

    /**
     * Handle RecyclerView's clicks
     */
    private void initClicks() {
        //Search button
        binding.btnSearch.setOnClickListener(v -> mViewModel.search(binding.etSearch.getText().toString()));
        //Adapter's click
        movieAdapter.setOnClickListener((pos, movie) -> {
            Intent intent = new Intent(MovieSearchActivity.this, MovieDetailActivity.class);
            intent.putExtra(ARG_MOVIE_ID, movie.getId());
            startActivity(intent);
        });
    }

    /**
     * Initialize observers for getting data from the ViewModel
     */
    private void initObservers() {
        mViewModel.getSearchList().observe(this, this::prepareRecycler);
        mViewModel.getSearchControl().observe(this, aBoolean -> {
            if (aBoolean)
                Toast.makeText(this, "You should enter at least one letter", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Set the data to the RecyclerView's adapter
     *
     * @param models as List<Result>
     */
    private void prepareRecycler(List<Result> models) {
        movieAdapter.setAdapterList(models);
    }
}