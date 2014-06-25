package org.codenut.app.magnifier;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GridViewFragment extends Fragment {
    private GridListAdapter mAdapter;
    private TextView mEmpty;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.grid_view_fragment, container, false);

        GridView gridView = (GridView) v.findViewById(R.id.grid_view);

        mAdapter = new GridListAdapter(getActivity(), R.layout.grid_item, findFiles());
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File file = mAdapter.getItem(position);
                FragmentManager fm = getActivity().getSupportFragmentManager();
                Fragment fragment = ImagePreviewFragment.newInstance(file);
                fm.beginTransaction()
                        .setCustomAnimations(R.animator.load_gallery, R.animator.unload_gallery, R.animator.load_gallery, R.animator.unload_gallery)
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss();
            }
        });
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
                adb.setTitle(getString(R.string.dialog_delete_title));
                adb.setMessage(getString(R.string.dialog_delete_text));
                final File fileToRemove = mAdapter.getItem(position);
                adb.setNegativeButton(getString(R.string.dialog_delete_button_cancel), null);
                adb.setPositiveButton(getString(R.string.dialog_delete_button_ok), new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mAdapter.remove(fileToRemove);
                    }
                });
                adb.show();
                return true;
            }
        });

        mEmpty = (TextView) v.findViewById(R.id.empty_gallery);
        showEmptyMessage();

        return v;
    }

    private void showEmptyMessage() {
        if (mAdapter.getCount() == 0) {
            mEmpty.setText(getString(R.string.gallery_empty));
            mEmpty.setVisibility(View.VISIBLE);
        }
    }

    private class GridListAdapter extends ArrayAdapter<File> {

        public GridListAdapter(Context context, int textViewResourceId, List<File> objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            showEmptyMessage();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final File file = getItem(position);

            final ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Service.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.grid_item, parent, false);
                holder = new ViewHolder();
                holder.imageView = (ImageView) convertView.findViewById(R.id.item);
                holder.textView = (TextView) convertView.findViewById(R.id.file_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final int width = (int) getActivity().getResources().getDimension(R.dimen.gallery_item_width);
            final int height = (int) getActivity().getResources().getDimension(R.dimen.gallery_item_height);

            holder.imageView.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
            holder.imageView.setImageDrawable(null);

            final String text = PreviewImage.getDatePart(file) + " " + PreviewImage.getTimePart(file);
            holder.textView.setText(text);
            new AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... params) {
                    return BitmapUtil.decodeSampledBitmapFromFile(file.getPath(), width, height);
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    holder.imageView.setImageBitmap(bitmap);
                }
            }.execute();

            return convertView;
        }

        @Override
        public void remove(File file) {
            if (file.delete()) {
                super.remove(file);
            }
        }

        private class ViewHolder {
            public ImageView imageView;
            public TextView textView;
        }

    }

}

