package mvasoft.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;

public class AlertDialogFragment extends BaseDialogFragment {

    private static final String ARGS_MSG_TEXT = "ARGS_RES_MSG";
    private static final String ARGS_REQ_CODE = "ARGS_REQ_CODE";

    private String mDialogMessage;
    private int mRequestCode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mDialogMessage = args.getString(ARGS_MSG_TEXT);
            mRequestCode = args.getInt(ARGS_REQ_CODE);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        b.setMessage(mDialogMessage);
        b.setPositiveButton(android.R.string.ok, (dialog, which) -> sendResult(new AlertDialogResultData(mRequestCode)));

        b.setNegativeButton(android.R.string.cancel, null);
        return b.create();
    }

    public static class AlertDialogResultData extends DialogResultData {

        AlertDialogResultData(int requestCode) {
            super(requestCode);
        }
    }

    public static class Builder extends BaseDialogFragment.Builder {

        private String messageText;

        public Builder(int requestCode) {
            super(requestCode);
        }

        @Override
        BaseDialogFragment newInstance() {
            return new AlertDialogFragment();
        }

        @Override
        Bundle makeArgs() {
            Bundle b = new Bundle();
            b.putInt(ARGS_REQ_CODE, requestCode);
            b.putString(ARGS_MSG_TEXT, messageText);
            return b;
        }

        public Builder withMessage(@NonNull String message) {
            messageText = message;
            return this;
        }
    }
}
