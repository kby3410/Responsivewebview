package com.example.responsivewebview;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class DBRecyclerAdapter extends RecyclerView.Adapter<DBRecyclerAdapter.ItemViewHolder> {
    private final ArrayList<DBData> listData;
    Context mContext;
    private DBHandler handler;



    public DBRecyclerAdapter(ArrayList<DBData> listData, Context context) {
        this.listData = listData;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dbitem, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.onBind(listData.get(position));
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView FileList;

        ItemViewHolder(View itemView) {
            super(itemView);

            FileList = itemView.findViewById(R.id.DBTextView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {       //해당 아이템 클릭 리스너
                    final int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        final Context context = v.getContext();
                        PopupMenu popup= new PopupMenu(context.getApplicationContext(), v);
                        popup.getMenuInflater().inflate(R.menu.option_menu, popup.getMenu());
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()){
                                    case R.id.m1:
                                        //Intent intent = new Intent(context.getApplicationContext(),list.class);
                                        //intent.putExtra("http",listData.get(pos).getHttp());
                                        //context.startActivity(intent);
                                        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                                        NetworkInfo ninfo = cm.getActiveNetworkInfo();
                                        if(ninfo == null) {
                                            Dialog("인터넷 연결을 확인해주세요.");
                                        }else {
                                            Intent intent = new Intent(context.getApplicationContext(), web.class);
                                            intent.putExtra("url", "https://" + listData.get(pos).getHttp());
                                            intent.putExtra("name", listData.get(pos).getName());
                                            intent.putExtra("check_number", listData.get(pos).getCheck_number());
                                            System.out.println("name_adapter = " + listData.get(pos).getName());
                                            System.out.println("check_adapter = " + listData.get(pos).getCheck_number());
                                            context.startActivity(intent);
                                            handler = DBHandler.open(context);
                                            handler.update(listData.get(pos).getHttp(), listData.get(pos).getName(), 1);
                                            System.out.println("1name_adapter = " + listData.get(pos).getName());
                                            System.out.println("1check_adapter = " + listData.get(pos).getCheck_number());
                                        }
                                        break;
                                    case R.id.m2:

                                        AlertDialog.Builder alert1 = new AlertDialog.Builder(context);
                                        View view = LayoutInflater.from(context).inflate(R.layout.db_change_dialog, null, false);
                                        alert1.setView(view);
                                        final Button ButtonSubmit = (Button)view.findViewById(R.id.mod_bt);
                                        final EditText editTextUrl = (EditText)view.findViewById(R.id.mod_url);
                                        final EditText editTextName = (EditText)view.findViewById(R.id.mod_name);

                                        editTextUrl.setText(listData.get(pos).getHttp());
                                        editTextName.setText(listData.get(pos).getName());

                                        final AlertDialog dialog = alert1.create();
                                        ButtonSubmit.setOnClickListener(new View.OnClickListener(){
                                            public void onClick(View v){
                                                handler = DBHandler.open(context);
                                                handler.delete(listData.get(pos).getName());
                                                String URL = editTextUrl.getText().toString();
                                                String NAME = editTextName.getText().toString();
                                                DBData save_data = new DBData(URL,NAME,0);
                                                listData.set(getAdapterPosition(), save_data);
                                                notifyItemChanged(getAdapterPosition());
                                                dialog.dismiss();
                                                handler.insert(URL,NAME,0);
                                            }
                                        });
                                        dialog.show();
                                        break;

                                    case R.id.m3:
                                        handler = DBHandler.open(context);
                                        AlertDialog.Builder alert = new AlertDialog.Builder(context);    //다이얼로그
                                        alert.setTitle("알림");
                                        alert.setMessage("해당URL를 삭제하시겠습니까?");
                                        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                handler.delete(listData.get(pos).getName());
                                                remove(pos);                            //클릭된 뷰 삭제 및 데이터 삭제
                                                dialog.dismiss();
                                            }
                                        });
                                        alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                Toast.makeText(context, "취소하였습니다.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        alert.show();
                                        break;
                                    default:
                                        break;
                                }
                                return false;
                            }
                        });
                        popup.show();
                    }
                }
            });
        }

        void onBind(DBData data) {
            FileList.setText(data.getName() + " (" + data.getHttp() + ")");
        }


        public void remove(int position) {                   //해당포지션 삭제
            try {
                listData.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, listData.size());
            } catch (IndexOutOfBoundsException ex) {
                ex.printStackTrace();
            }
        }
    }

    void Dialog(String text){
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);    //다이얼로그
        alert.setTitle("알림");
        alert.setMessage(text);

        alert.setCancelable(false);
        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        alert.show();
    }
}
