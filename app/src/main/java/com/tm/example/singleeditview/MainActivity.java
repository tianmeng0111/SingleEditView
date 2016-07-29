package com.tm.example.singleeditview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_vertify;
    private SingleEditView sev_pwd;
    private ImageView iv_empty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        btn_vertify = (Button) findViewById(R.id.btn_vertify);

        btn_vertify.setOnClickListener(this);
        sev_pwd = (SingleEditView) findViewById(R.id.sev_pwd);
        iv_empty = (ImageView) findViewById(R.id.iv_empty);
        iv_empty.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_vertify:
                if (sev_pwd.isEditDown()) {
                    Toast.makeText(MainActivity.this, "vertify!" + sev_pwd.getEditText(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "please complete password!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.iv_empty:
                sev_pwd.clearEdit();
                break;
        }
    }
}
