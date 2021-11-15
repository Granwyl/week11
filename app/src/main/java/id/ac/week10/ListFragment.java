package id.ac.week10;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListFragment extends Fragment implements LoadMahasiswaAsync.LoadMahasiswaCallback,
DeleteMahasiswaAsync.DeleteMahasiswaCallback{

    public static final String EXTRA_MHS_LIST = "EXTRA_MHS_LIST";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    RecyclerView rvMhs;
    ArrayList<Mahasiswa> arrMhs=new ArrayList<>();
    AppDatabase db;
    MahasiswaAdapter adapter;
    public ListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ListFragment newInstance(String param1, String param2) {
        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getAppDatabase(getActivity());
        rvMhs  =  view.findViewById(R.id.recyclerView_mhs);
        rvMhs.setHasFixedSize(true);
        rvMhs.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter= new MahasiswaAdapter(arrMhs);
        rvMhs.setAdapter(adapter);
        adapter.setOnClickedCallback(new MahasiswaAdapter.OnClickedCallback() {
            @Override
            public void onClick(Mahasiswa mhs) {
                new DeleteMahasiswaAsync(getActivity(),ListFragment.this,mhs).execute();
            }
        });
        //new LoadMahasiswaTask().execute(); //panggil load data Mahasiswa dari database
        new LoadMahasiswaAsync(getActivity(),ListFragment.this).execute();
    }

    @Override
    public void preexecute() {

    }

    @Override
    public void postexecute(List<Mahasiswa> listmhs) {
        arrMhs.clear();
        arrMhs.addAll(listmhs);
        adapter.notifyDataSetChanged();
        //Toast.makeText(getActivity(), "List Mahasiswa sukses", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void preexecutedelete() {

    }

    @Override
    public void postexecutedelete() {
        Toast.makeText(getActivity(), "Berhasil delete", Toast.LENGTH_SHORT).show();
        adapter.notifyDataSetChanged();
        //load ulang dari database, bisa juga hapus dari arraylist
        new LoadMahasiswaAsync(getActivity(),ListFragment.this).execute();
    }

    //input, progress, outout
    private class LoadMahasiswaTask extends AsyncTask<Void,Void, List<Mahasiswa>> {
        @Override
        protected List<Mahasiswa> doInBackground(Void... voids) {
            return db.mahasiswaDAO().getAllMhs();
        }
        @Override
        protected void onPostExecute(List<Mahasiswa> mahasiswas) { //mahasiswas adalah hasil doInBackground
            super.onPostExecute(mahasiswas);
            //selesai load data mahasiswa
            arrMhs.clear();
            arrMhs.addAll(mahasiswas);
            adapter.notifyDataSetChanged();
        }
    }

}

class LoadMahasiswaAsync {
    private final WeakReference<Context>  weakcontext;
    private final WeakReference<LoadMahasiswaCallback>  weakCallback;

    public LoadMahasiswaAsync(Context weakcontext, LoadMahasiswaCallback weakCallback) {
        this.weakcontext = new WeakReference<>(weakcontext);
        this.weakCallback = new WeakReference<>(weakCallback);
    }

    void execute(){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        weakCallback.get().preexecute();
        executorService.execute(()->{
            Context c = weakcontext.get();
            List<Mahasiswa> result = AppDatabase.getAppDatabase(c).mahasiswaDAO().getAllMhs();
            handler.post(()->weakCallback.get().postexecute(result));
        });
    }
    interface LoadMahasiswaCallback{
        void preexecute();
        void postexecute(List<Mahasiswa> listmhs);
    }
}

class DeleteMahasiswaAsync {
    private final WeakReference<Context>  weakcontext;
    private final WeakReference<DeleteMahasiswaCallback>  weakCallback;
    private Mahasiswa mhs;

    public DeleteMahasiswaAsync(Context weakcontext, DeleteMahasiswaCallback weakCallback,Mahasiswa m) {
        this.weakcontext = new WeakReference<>(weakcontext);
        this.weakCallback = new WeakReference<>(weakCallback);
        this.mhs = m;
    }

    void execute(){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        weakCallback.get().preexecutedelete();
        executorService.execute(()->{
            Context c = weakcontext.get();
            AppDatabase.getAppDatabase(c).mahasiswaDAO().delete(mhs);
            handler.post(()->weakCallback.get().postexecutedelete());
        });
    }
    interface DeleteMahasiswaCallback{
        void preexecutedelete();
        void postexecutedelete();
    }
}