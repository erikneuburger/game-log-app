package com.tep.gamelog;

//import android.app.ProgressDialog;
import android.app.AlertDialog;
import android.content.Context;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.SearchView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tep.gamelog.model.Game;
import com.tep.gamelog.service.RetrofitConfig;
import com.tep.gamelog.service.GameService;
import com.tep.gamelog.model.GameDAO;
import com.tep.gamelog.model.GameSQLite;
import com.tep.gamelog.util.DBUtil;

import java.util.List;


public class SearchActivity extends AppCompatActivity {

    private EditText searchText;
    private TextView resultText;
    private SearchView searchView;
    public String queryText;
    public Toast toast;

    private GameSQLite gameatual = null;
    private GameDAO dao;
    private List<GameSQLite> games;
    private boolean contemGame = false;
    private List<Game> gamelist;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        FloatingActionButton addButton = findViewById(R.id.add_button);
        FloatingActionButton backButton = findViewById(R.id.back_button);
        searchText = findViewById(R.id.search_text);
        resultText = findViewById(R.id.search_result_text);
        searchView = findViewById(R.id.search_view);
        searchView.setSubmitButtonEnabled(true);
        searchView.setIconifiedByDefault(false);
        addButton.setEnabled(false);

        final DBUtil dbutil = DBUtil.getInstance(this.getApplicationContext());
        dao = new GameDAO(dbutil.getDb());
        games = dao.lista();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("Query", "QUERY DE PESQUISA" + query);

                queryText = query.toUpperCase();

                Log.d("Query up", "QUERY TO UPPER" + queryText);

                AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
                builder.setCancelable(false); // if you want user to wait for some process to finish,
                builder.setView(R.layout.dialog_loading);
                builder.setTitle("Game Log API v 1.0");
                AlertDialog dialog = builder.create();

                dialog.show(); // to show this dialog

                GameService service = RetrofitConfig.getRetrofitInstance().create(GameService.class);
                Call<List<Game>> call = service.findGameTitle(queryText);
                call.enqueue(new Callback<List<Game>>() {
                    //Call<List<Game>> call = new RetrofitConfig().getGameService().listGames();
                    //call.enqueue(new Callback<List<Game>>() {
                    @Override
                    public void onResponse(Call<List<Game>> call, Response<List<Game>> response) {
                        Log.d("onResponse", "Entrou no onResponse");
                        dialog.dismiss(); // to hide this dialog
                        gamelist = response.body();
                        Log.d("Teste", "GameList" + gamelist.toString());
                        if (!gamelist.isEmpty()) {

                            addButton.setEnabled(true);

                            resultText.setText(gamelist.get(0).getId() +
                                    "\n" + gamelist.get(0).getTitle() +
                                    "\n" + gamelist.get(0).getRelease());

                        }else{
                            resultText.setText("Nenhum jogo encontrado");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Game>> call, Throwable t) {

                        dialog.dismiss(); // to hide this dialog
                        resultText.setText("Erro ao buscar dados na API");
                        Log.e("GameService   ", "Erro ao buscar dados na API:" + t.getMessage());
                    }
                });

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Log.d("Query", "QUERY DE PESQUISA ON TEXT CHANGE" + newText);
                return false;
            }

        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                games = dao.lista();
                contemGame = false;

                if(gameatual == null)
                    gameatual = new GameSQLite();

                gameatual.setId(gamelist.get(0).getId());
                gameatual.setTitle(gamelist.get(0).getTitle());
                gameatual.setRelease("" + gamelist.get(0).getRelease());

                if(games != null){

                    for (GameSQLite game:games){

                        if(game.getId().equals(gameatual.getId())){
                            //dao.alterar(gameatual);
                            contemGame = true;
                            Log.d("ER", "alterar");
                            toast.makeText(SearchActivity.this, "Registro alterado com sucesso", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                if(gameatual.getId() != "" && contemGame == false) {
                    dao.inserir(gameatual);
                    Log.d("ER", "inserir");
                    //Toast.makeText(SearchActivity.this, "Registro inserido com sucesso", Toast.LENGTH_SHORT).show();
                }

                Intent i = new Intent(SearchActivity.this, MainActivity.class);
                startActivity(i);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(SearchActivity.this, MainActivity.class);
                startActivity(i);
            }
        });
    }
}
