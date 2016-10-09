package com.bopr.filechooser;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class FileChooserFragment extends DialogFragment {

    private static final String TAG = "FileChooserFragment";

    public static final String UP_DIR = "..";

    public static final String ARG_FILTER_EXTENSIONS = "filter_extensions";
    public static final String ARG_FILE_NAME = "file_name";
    public static final String ARG_ALLOW_FILE_OPERATIONS = "allow_file_operations";
    public static final String ARG_SHOW_FILE_TIME = "show_file_time";

    private TextView currentPathView;
    private RecyclerView listView;
    private FloatingActionButton addButton;
    private OnDismissListener onDismissListener;
    private FileFilter filter;
    private boolean cancelled;
    private File currentPath;
    private File selectedFile;
    private boolean allowFileOperations;
    private boolean showFileTime;
    private FileContextMenuCreator fileContextMenuCreator;
    private FolderContextMenuCreator folderContextMenuCreator;

    public static FileChooserFragment newInstance(boolean allowFileOperations,
                                                  String fileName,
                                                  boolean showFileDetails,
                                                  String[] filterExtensions) {
        FileChooserFragment fragment = new FileChooserFragment();

        Bundle args = new Bundle();
        args.putBoolean(ARG_ALLOW_FILE_OPERATIONS, allowFileOperations);
        args.putBoolean(ARG_SHOW_FILE_TIME, showFileDetails);
        args.putString(ARG_FILE_NAME, fileName);
        args.putStringArray(ARG_FILTER_EXTENSIONS, filterExtensions);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fileContextMenuCreator = new FileContextMenuCreator();
        folderContextMenuCreator = new FolderContextMenuCreator();
        allowFileOperations = getArguments().getBoolean(ARG_ALLOW_FILE_OPERATIONS);
        showFileTime = getArguments().getBoolean(ARG_SHOW_FILE_TIME);

        cancelled = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_chooser, container, false);

        if (allowFileOperations) {
            registerForContextMenu(view);
        }

        currentPathView = (TextView) view.findViewById(R.id.label_current);

        listView = (RecyclerView) view.findViewById(android.R.id.list);
        listView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        addButton = (FloatingActionButton) view.findViewById(R.id.button_add);
        addButton.setVisibility(View.GONE);
        addButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                createFile();
            }
        });

        init();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (allowFileOperations) {
            addButton.show();
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (onDismissListener != null) {
            String path = cancelled ? currentPath.getAbsolutePath() : selectedFile.getAbsolutePath();
            Log.d(TAG, "dismiss. selected: " + path);
            onDismissListener.onDismiss(path, cancelled);
        }
    }

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    private void setCurrentPath(File file) {
        File path;
        if (isUpFolder(file)) {
            path = file.getParentFile().getParentFile();
        } else if (file.isDirectory()) {
            path = file;
        } else {
            path = file.getParentFile();
        }

        try {
            currentPath = path.getCanonicalFile();
            if (currentPath.getName().isEmpty()) {
                currentPath = new File("/");
            }
        } catch (IOException x) {
            throw new RuntimeException(x);
        }

        currentPathView.setText(currentPath.getAbsolutePath());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.menu_file_chooser, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_delete) {
            deleteFile();
        } else if (i == R.id.action_rename) {
            renameFile();
        } else if (i == R.id.action_create_folder) {
            createFolder();
        }
        return super.onContextItemSelected(item);
    }

    private void init() {
        final String[] extensions = getArguments().getStringArray(ARG_FILTER_EXTENSIONS);
        filter = new FileFilter() {

            @Override
            public boolean accept(File file) {
                if (extensions != null && !file.isDirectory()) {
                    for (String ext : extensions) {
                        if (file.getName().toLowerCase(Locale.getDefault()).endsWith(ext)) {
                            return true;
                        }
                    }
                    return false;
                } else {
                    return true;
                }
            }
        };

        File path = Environment.getExternalStorageDirectory();
        String fileName = getArguments().getString(ARG_FILE_NAME);
        if (fileName != null) {
            File file = new File(fileName);
            if (file.exists()) {
                path = file;
            }
        }

        Log.d(TAG, "init. startup path: " + path);

        setCurrentPath(path);
        reload();
    }

    private void close() {
        cancelled = false;
        dismiss();
    }

    private void reload() {
        List<File> list = new ArrayList<>();
        if (currentPath.getParent() != null) {
            list.add(new File(currentPath, UP_DIR));
        }

        File[] files = currentPath.listFiles(filter);
        if (files != null) {
            Collections.addAll(list, files);
        }
        Collections.sort(list, new FileComparator());

        listView.setAdapter(new FileListAdapter(getContext(), list));
    }

    private void createFile() {
        final EditText input = createFilenameEditor();
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.app_name)
                .setView(input)
                .setMessage(R.string.create_file)
                .setPositiveButton(R.string.action_create, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedFile = new File(currentPath, input.getText().toString());
                        close();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    private void createFolder() {
        final EditText input = createFilenameEditor();
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.app_name)
                .setView(input)
                .setMessage(R.string.create_folder)
                .setPositiveButton(R.string.action_create, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File folder = new File(currentPath, input.getText().toString());
                        if (folder.mkdirs()) {
                            reload();
                        } else {
                            Toast.makeText(getContext(), R.string.cannot_create_folder, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    private void deleteFile() {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.app_name)
                .setMessage(selectedFile.isDirectory() ? R.string.delete_folder_confirm_message : R.string.delete_file_confirm_message)
                .setPositiveButton(R.string.action_delete_file, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (deleteFilesRecursively(selectedFile)) {
                            reload();
                        } else {
                            Toast.makeText(getContext(), selectedFile.isDirectory()
                                    ? R.string.cannot_delete_folder_message
                                    : R.string.cannot_delete_file_message, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    private void renameFile() {
        final EditText input = createFilenameEditor();
        input.setText(selectedFile.getName());
        input.selectAll();

        new AlertDialog.Builder(getContext())
                .setTitle(R.string.app_name)
                .setMessage(selectedFile.isDirectory() ? R.string.rename_folder_message : R.string.rename_file_message)
                .setPositiveButton(R.string.action_rename_file, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = input.getText().toString();
                        if (selectedFile.renameTo(new File(selectedFile.getParentFile(), name))) {
                            reload();
                        } else {
                            Toast.makeText(getContext(), selectedFile.isDirectory()
                                    ? R.string.cannot_rename_file
                                    : R.string.cannot_rename_folder, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    @NonNull
    private EditText createFilenameEditor() {
        final EditText input = new EditText(getContext());
        input.setPadding(48, 0, 48, 0);
        input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        return input;
    }

    private boolean deleteFilesRecursively(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                if (!f.delete()) {
                    return false;
                }
            }
        }
        return file.delete();
    }

    private boolean isUpFolder(File file) {
        return file.getName().equals(UP_DIR);
    }

    private class FileListAdapter extends RecyclerView.Adapter<ItemViewHolder> {

        private Context context;
        private List<File> files;
        private DateFormat fileDateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.DEFAULT);

        private FileListAdapter(Context context, List<File> files) {
            this.context = context;
            this.files = files;
        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            return new ItemViewHolder(inflater.inflate(R.layout.list_item_file, parent, false));
        }

        @Override
        public void onBindViewHolder(ItemViewHolder holder, int position) {
            if (files != null) {
                final File file = files.get(position);

                holder.nameView.setText(file.getName());

                if (isUpFolder(file)) {
                    holder.iconView.setImageResource(R.drawable.ic_folder_up);
                    holder.dateView.setText(null);
                } else {
                    holder.dateView.setText(fileDateFormat.format(file.lastModified()));
                    if (file.isDirectory()) {
                        holder.iconView.setImageResource(R.drawable.ic_folder);
                        holder.itemView.setOnCreateContextMenuListener(folderContextMenuCreator);
                    } else {
                        holder.iconView.setImageResource(R.drawable.ic_file);
                        holder.itemView.setOnCreateContextMenuListener(fileContextMenuCreator);
                    }
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (file.isFile()) {
                            selectedFile = file;
                            close();
                        } else {
                            setCurrentPath(file);
                            reload();
                        }
                    }
                });

                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        selectedFile = file;
                        return false;
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return files == null ? 0 : files.size();
        }

    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        private final ImageView iconView;
        private final TextView nameView;
        private final TextView dateView;

        private ItemViewHolder(View view) {
            super(view);

            iconView = (ImageView) view.findViewById(R.id.image_file);
            nameView = (TextView) view.findViewById(R.id.label_file_name);
            dateView = (TextView) view.findViewById(R.id.label_file_time);
            dateView.setVisibility(showFileTime ? View.VISIBLE : View.GONE);
        }

    }

    private class FileComparator implements Comparator<File> {

        @Override
        public int compare(File f1, File f2) {
            if (f1.isDirectory()) {
                return f2.isDirectory() ? compareName(f1, f2) : -1;
            } else {
                return f2.isDirectory() ? 1 : compareName(f1, f2);
            }
        }

        private int compareName(File f1, File f2) {
            return f1.getName().compareTo(f2.getName());
        }
    }

    private class FileContextMenuCreator implements View.OnCreateContextMenuListener {

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            getActivity().getMenuInflater().inflate(R.menu.menu_file_chooser_file, menu);
        }
    }

    private class FolderContextMenuCreator implements View.OnCreateContextMenuListener {

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            getActivity().getMenuInflater().inflate(R.menu.menu_file_chooser_folder, menu);
        }
    }

    public interface OnDismissListener {

        void onDismiss(String selectedFileName, boolean cancelled);
    }

}
