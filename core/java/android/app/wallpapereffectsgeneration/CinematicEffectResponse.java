/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.app.wallpapereffectsgeneration;

import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A {@link CinematicEffectResponse} include textured meshes
 * and camera attributes of key frames.
 *
 * @hide
 */
@SystemApi
public final class CinematicEffectResponse implements Parcelable {
    /** @hide */
    @IntDef(prefix = {"CINEMATIC_EFFECT_STATUS_"},
            value = {CINEMATIC_EFFECT_STATUS_UNKNOWN,
                    CINEMATIC_EFFECT_STATUS_OK,
                    CINEMATIC_EFFECT_STATUS_ERROR,
                    CINEMATIC_EFFECT_STATUS_NOT_READY,
                    CINEMATIC_EFFECT_STATUS_PENDING,
                    CINEMATIC_EFFECT_STATUS_TOO_MANY_REQUESTS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CinematicEffectStatusCode {}

    /** Cinematic effect generation unknown status. */
    public static final int CINEMATIC_EFFECT_STATUS_UNKNOWN = 0;
    /** Cinematic effect generation success. */
    public static final int CINEMATIC_EFFECT_STATUS_OK = 1;
    /** Cinematic effect generation failure. */
    public static final int CINEMATIC_EFFECT_STATUS_ERROR = 2;
    /** Service not ready for cinematic effect generation. */
    public static final int CINEMATIC_EFFECT_STATUS_NOT_READY = 3;
    /** Cienmatic effect generation process is pending. */
    public static final int CINEMATIC_EFFECT_STATUS_PENDING = 4;
    /** Too manay requests for server to handle. */
    public static final int CINEMATIC_EFFECT_STATUS_TOO_MANY_REQUESTS = 5;

    /** @hide */
    @IntDef(prefix = {"IMAGE_CONTENT_TYPE_"},
            value = {IMAGE_CONTENT_TYPE_UNKNOWN,
                    IMAGE_CONTENT_TYPE_PEOPLE_PORTRAIT,
                    IMAGE_CONTENT_TYPE_LANDSCAPE,
                    IMAGE_CONTENT_TYPE_OTHER
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ImageContentType {}

    /** Image content unknown. */
    public static final int IMAGE_CONTENT_TYPE_UNKNOWN = 0;
    /** Image content is people portrait. */
    public static final int IMAGE_CONTENT_TYPE_PEOPLE_PORTRAIT = 1;
    /** Image content is landscape. */
    public static final int IMAGE_CONTENT_TYPE_LANDSCAPE = 2;
    /** Image content is doesn't belong to other types. */
    public static final int IMAGE_CONTENT_TYPE_OTHER = 3;


    @CinematicEffectStatusCode
    private int mStatusCode;

    /** The id of the cinematic effect generation task. */
    @NonNull
    private String mTaskId;

    @ImageContentType
    private int mImageContentType;

    /** The textured mesh required to render cinematic effect. */
    @NonNull
    private List<TexturedMesh> mTexturedMeshes;

    /** The start camera position for animation. */
    @Nullable
    private CameraAttributes mStartKeyFrame;

    /** The end camera position for animation. */
    @Nullable
    private CameraAttributes mEndKeyFrame;

    private CinematicEffectResponse(Parcel in) {
        mStatusCode = in.readInt();
        mTaskId = in.readString();
        mImageContentType = in.readInt();
        mTexturedMeshes = new ArrayList<TexturedMesh>();
        in.readTypedList(mTexturedMeshes, TexturedMesh.CREATOR);
        mStartKeyFrame = in.readTypedObject(CameraAttributes.CREATOR);
        mEndKeyFrame = in.readTypedObject(CameraAttributes.CREATOR);
    }

    private CinematicEffectResponse(@CinematicEffectStatusCode int statusCode,
            String taskId,
            @ImageContentType int imageContentType,
            List<TexturedMesh> texturedMeshes,
            CameraAttributes startKeyFrame,
            CameraAttributes endKeyFrame) {
        mStatusCode = statusCode;
        mTaskId = taskId;
        mImageContentType = imageContentType;
        mStartKeyFrame = startKeyFrame;
        mEndKeyFrame = endKeyFrame;
        mTexturedMeshes = texturedMeshes;
    }

    /** Gets the cinematic effect generation status code. */
    @CinematicEffectStatusCode
    public int getStatusCode() {
        return mStatusCode;
    }

    /** Get the task id. */
    @NonNull
    public String getTaskId() {
        return mTaskId;
    }

    /**
     * Get the image content type, which briefly classifies what's
     * the content of image, like people portrait, landscape etc.
     */
    @ImageContentType
    public int getImageContentType() {
        return mImageContentType;
    }

    /** Get the textured meshes. */
    @NonNull
    public List<TexturedMesh> getTexturedMeshes() {
        return mTexturedMeshes;
    }

    /**
     * Get the camera attributes (position info and other parameters, see docs of
     * {@link CameraAttributes}) of the start key frame on the animation path.
     */
    @Nullable
    public CameraAttributes getStartKeyFrame() {
        return mStartKeyFrame;
    }

    /**
     * Get the camera attributes (position info and other parameters, see docs of
     * {@link CameraAttributes}) of the end key frame on the animation path.
     */
    @Nullable
    public CameraAttributes getEndKeyFrame() {
        return mEndKeyFrame;
    }

    @NonNull
    public static final Creator<CinematicEffectResponse> CREATOR =
            new Creator<CinematicEffectResponse>() {
                @Override
                public CinematicEffectResponse createFromParcel(Parcel in) {
                    return new CinematicEffectResponse(in);
                }

                @Override
                public CinematicEffectResponse[] newArray(int size) {
                    return new CinematicEffectResponse[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel out, int flags) {
        out.writeInt(mStatusCode);
        out.writeString(mTaskId);
        out.writeInt(mImageContentType);
        out.writeTypedList(mTexturedMeshes, flags);
        out.writeTypedObject(mStartKeyFrame, flags);
        out.writeTypedObject(mEndKeyFrame, flags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CinematicEffectResponse that = (CinematicEffectResponse) o;
        return mTaskId.equals(that.mTaskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mTaskId);
    }
    /**
     * Builder of {@link CinematicEffectResponse}
     *
     * @hide
     */
    @SystemApi
    public static final class Builder {
        @CinematicEffectStatusCode
        private int mStatusCode;
        @NonNull
        private String mTaskId;
        @ImageContentType
        private int mImageContentType;
        @NonNull
        private List<TexturedMesh> mTexturedMeshes;
        @Nullable
        private CameraAttributes mStartKeyFrame;
        @Nullable
        private CameraAttributes mEndKeyFrame;

        /**
         * Constructor with task id and status code.
         *
         * @hide
         */
        @SystemApi
        public Builder(@CinematicEffectStatusCode int statusCode, @NonNull String taskId) {
            mStatusCode = statusCode;
            mTaskId = taskId;
        }

        /**
         * Sets the image content type.
         */
        @NonNull
        public Builder setImageContentType(@ImageContentType int imageContentType) {
            mImageContentType = imageContentType;
            return this;
        }


        /**
         * Sets the textured meshes.
         */
        @NonNull
        public Builder setTexturedMeshes(@NonNull List<TexturedMesh> texturedMeshes) {
            mTexturedMeshes = texturedMeshes;
            return this;
        }

        /**
         * Sets start key frame.
         */
        @NonNull
        public Builder setStartKeyFrame(@Nullable CameraAttributes startKeyFrame) {
            mStartKeyFrame = startKeyFrame;
            return this;
        }

        /**
         * Sets end key frame.
         */
        @NonNull
        public Builder setEndKeyFrame(@Nullable CameraAttributes endKeyFrame) {
            mEndKeyFrame = endKeyFrame;
            return this;
        }

        /**
         * Builds a {@link CinematicEffectResponse} instance.
         */
        @NonNull
        public CinematicEffectResponse build() {
            if (mTexturedMeshes == null) {
                // Place holder because build doesn't allow list to be nullable.
                mTexturedMeshes = new ArrayList<>(0);
            }
            return new CinematicEffectResponse(mStatusCode, mTaskId, mImageContentType,
                    mTexturedMeshes, mStartKeyFrame, mEndKeyFrame);
        }
    }
}