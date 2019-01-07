package mvasoft.dialogs;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

public class BaseDialogFragment extends DialogFragment{

    void sendResult(DialogResultData data) {
        Fragment fragment = getParentFragment();
        if (fragment instanceof DialogResultListener)
            ((DialogResultListener) fragment).onDialogResult(data);
        else if (getActivity() instanceof DialogResultListener) {
            ((DialogResultListener) getActivity()).onDialogResult(data);
        }
    }

    public abstract static class Builder {

        int requestCode;

        public Builder(int requestCode) {
            this.requestCode = requestCode;
        }

        public <T extends Fragment & DialogResultListener> void
        show(@NonNull T parentFragment, @NonNull String tag) {
            show(parentFragment.getChildFragmentManager(), tag);
        }


        public <T extends FragmentActivity & DialogResultListener> void
        show(@NonNull T parentActivity, @NonNull String tag) {
            show(parentActivity.getSupportFragmentManager(), tag);
        }

        private void show(@NonNull FragmentManager fm, @NonNull String tag) {
            if (fm.findFragmentByTag(tag) != null)
                return;

            BaseDialogFragment f = newInstance();
            f.setArguments(makeArgs());
            f.show(fm, tag);
        }

        abstract BaseDialogFragment newInstance();
        abstract Bundle makeArgs();
    }

}
