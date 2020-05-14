package ac.id.umn.queryfirestore;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;

public class MasterModel implements Parcelable {
    private String author;
    private String desc;
    private String mDate;
    private int status;
    private String title;
    private Timestamp uDate;
    private boolean doc;

    public MasterModel() {}

    public MasterModel(String author, String desc, String mDate, int status, String title, Timestamp uDate, boolean doc) {
        this.author = author;   this.desc = desc;
        this.mDate = mDate;     this.status = status;
        this.title = title;     this.uDate = uDate;
        this.doc = doc;
    }

    protected MasterModel(Parcel in) {
        author = in.readString();
        desc = in.readString();
        mDate = in.readString();
        status = in.readInt();
        title = in.readString();
        uDate = in.readParcelable(Timestamp.class.getClassLoader());
        doc = in.readByte() != 0;
    }

    public static final Creator<MasterModel> CREATOR = new Creator<MasterModel>() {
        @Override
        public MasterModel createFromParcel(Parcel in) {
            return new MasterModel(in);
        }

        @Override
        public MasterModel[] newArray(int size) {
            return new MasterModel[size];
        }
    };

    public String getAuthor() { return author; }

    public String getDesc() { return desc; }

    public String getmDate() { return mDate; }

    public int getStatus() { return status; }

    public String getTitle() { return title; }

    public Timestamp getuDate() { return uDate; }

    public boolean getDoc() { return doc; }

    public void setDoc(boolean doc) {
        this.doc = doc;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(author);
        parcel.writeString(desc);
        parcel.writeString(mDate);
        parcel.writeInt(status);
        parcel.writeString(title);
        parcel.writeParcelable(uDate, i);
        parcel.writeByte((byte) (doc ? 1 : 0));
    }
}