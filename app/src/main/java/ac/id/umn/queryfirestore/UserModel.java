package ac.id.umn.queryfirestore;

import android.os.Parcel;
import android.os.Parcelable;

public class UserModel implements Parcelable {
    private String fName;
    private String lName;
    private String address;
    //private String picId;
    private String bod;
    private int role;

    public UserModel() { }

    public UserModel(String fName, String lName, String address, String bod, int role) {
        this.fName = fName;
        this.lName = lName;
        this.address = address;
        //this.picId = picId;
        this.bod = bod;
        this.role = role;
    }

    protected UserModel(Parcel in) {
        fName = in.readString();
        lName = in.readString();
        address = in.readString();
        bod = in.readString();
        role = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fName);
        dest.writeString(lName);
        dest.writeString(address);
        dest.writeString(bod);
        dest.writeInt(role);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UserModel> CREATOR = new Creator<UserModel>() {
        @Override
        public UserModel createFromParcel(Parcel in) {
            return new UserModel(in);
        }

        @Override
        public UserModel[] newArray(int size) {
            return new UserModel[size];
        }
    };

    public String getfName() {
        return fName;
    }

    public String getlName() {
        return lName;
    }

    public String getAddress() {
        return address;
    }

//    public String getPicId() {
//        return picId;
//    }

    public String getBod() {
        return bod;
    }

    public int getRole() {
        return role;
    }
}
