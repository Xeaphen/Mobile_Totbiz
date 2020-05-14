package ac.id.umn.queryfirestore;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;

@GlideModule
public class GlideActivity extends AppGlideModule {

    @Override
    public void registerComponents(@NonNull Context context, @NonNull com.bumptech.glide.Glide glide,
                                   @NonNull Registry registry) {
//        super.registerComponents(context, glide, registry);
        registry.append(StorageReference.class, InputStream.class, new FirebaseImageLoader.Factory());
    }
}
