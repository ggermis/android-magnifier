package org.codenut.app.magnifier;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;

public class ListViewFragment extends ListFragment {
    private ArrayList<File> mFiles;
    private ListItemAdapter mAdapter;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFiles = findFiles();
        mAdapter = new ListItemAdapter(mFiles);
        setListAdapter(mAdapter);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
                adb.setTitle("Deleting Image");
                adb.setMessage("Are you sure you ?");
                final int positionToRemove = position;
                adb.setNegativeButton("Cancel", null);
                adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mAdapter.remove(mFiles.get(positionToRemove));
                    }
                });
                adb.show();
            }
        });
    }

    private ArrayList<File> findFiles() {
        return new ArrayList<File>(Arrays.asList(getActivity().getFilesDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".jpg");
            }
        })));
    }

    private class ListItemAdapter extends ArrayAdapter<File> {
        public ListItemAdapter(ArrayList<File> files) {
            super(getActivity(), 0, files);
            setNotifyOnChange(true);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_file, null);
            }

            File file = getItem(position);

            ImageView imageView = (ImageView) convertView.findViewById(R.id.list_item_image);
            imageView.setImageBitmap(BitmapUtil.decodeSampledBitmapFromFile(file.getPath(), 800, 600));

            return convertView;
        }

        @Override
        public void remove(File file) {
            file.delete();
            super.remove(file);
        }
    }
}
