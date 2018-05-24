package mvasoft.dialogs;

public abstract class DialogResultData {
    public final int requestCode;

    DialogResultData(int requestCode) {
        this.requestCode = requestCode;
    }
}
