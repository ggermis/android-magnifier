package org.codenut.app.magnifier;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.ListFragmentLayout;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ImageListFragment extends ListFragment {
    private ArrayList<File> mFiles;
    private ListItemAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.image_list_fragment, container, false);
        ListFragmentLayout.setupIds(view);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFiles = findFiles();
        mAdapter = new ListItemAdapter(mFiles);
        setListAdapter(mAdapter);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File file = (File) getListAdapter().getItem(position);
                FragmentManager fm = getActivity().getSupportFragmentManager();
                Fragment fragment = ImagePreviewFragment.newInstance(file);
                fm.beginTransaction()
                        .setCustomAnimations(R.animator.load_gallery, R.animator.unload_gallery, R.animator.load_gallery, R.animator.unload_gallery)
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss();
            }
        });
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
                adb.setTitle("Deleting Image");
                adb.setMessage("Are you sure you ?");
                final File fileToRemove = mFiles.get(position);
                adb.setNegativeButton("Cancel", null);
                adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mAdapter.remove(fileToRemove);
                    }
                });
                adb.show();
                return true;
            }
        });
    }

    private ArrayList<File> findFiles() {
        File[] files = getActivity().getFilesDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".jpg");
            }
        });
        Arrays.sort(files, Collections.reverseOrder());
        return new ArrayList<File>(Arrays.asList(files));
    }

    private class ListItemAdapter extends ArrayAdapter<File> {
        public ListItemAdapter(ArrayList<File> files) {
            super(getActivity(), 0, files);
            setNotifyOnChange(true);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.image_list_item, null);
                ListItemViewHolder holder = new ListItemViewHolder();
                holder.imageView = (ImageView) convertView.findViewById(R.id.list_item_image);
                convertView.setTag(holder);
            }

            final ListItemViewHolder holder = (ListItemViewHolder) convertView.getTag();
            final File file = getItem(position);
            new AsyncTask<ListItemViewHolder, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(ListItemViewHolder... params) {
                    WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
                    Display display = wm.getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    return BitmapUtil.decodeSampledBitmapFromFile(file.getPath(), size.x, size.y);
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    holder.imageView.setImageBitmap(bitmap);
                }
            }.execute(holder);

            return convertView;
        }

        @Override
        public void remove(File file) {
            if (file.delete()) {
                super.remove(file);
            }
        }
    }

    private class ListItemViewHolder {
        ImageView imageView;
    }

}
