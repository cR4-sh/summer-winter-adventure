package dev.swa.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;

record LessonProgress(boolean enrolled, boolean heardEcho, boolean sawControl,
        Prediction prediction, Comparison comparison) {
    static final LessonProgress EMPTY = new LessonProgress(false, false, false, Prediction.NONE, Comparison.UNCHECKED);

    static final Codec<LessonProgress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("enrolled", false).forGetter(LessonProgress::enrolled),
            Codec.BOOL.optionalFieldOf("heard_echo", false).forGetter(LessonProgress::heardEcho),
            Codec.BOOL.optionalFieldOf("saw_control", false).forGetter(LessonProgress::sawControl),
            StringRepresentable.fromEnum(Prediction::values)
                    .optionalFieldOf("prediction", Prediction.NONE).forGetter(LessonProgress::prediction),
            StringRepresentable.fromEnum(Comparison::values)
                    .optionalFieldOf("comparison", Comparison.UNCHECKED).forGetter(LessonProgress::comparison)
    ).apply(instance, LessonProgress::new));

    LessonProgress enroll() {
        return new LessonProgress(true, heardEcho, sawControl, prediction, comparison);
    }

    LessonProgress observeEcho() {
        return new LessonProgress(enrolled, true, sawControl, prediction, comparison);
    }

    LessonProgress observeControl() {
        return new LessonProgress(enrolled, heardEcho, true, prediction, comparison);
    }

    LessonProgress predict(Prediction answer) {
        return new LessonProgress(enrolled, heardEcho, sawControl, answer, comparison);
    }

    LessonProgress checkToken() {
        return switch (prediction) {
            case NONE -> this;
            case MATERIAL -> new LessonProgress(enrolled, heardEcho, sawControl, Prediction.NONE, Comparison.CONTRADICTED);
            case PERSONAL_HISTORY -> new LessonProgress(enrolled, heardEcho, sawControl, prediction, Comparison.VERIFIED);
        };
    }

    enum Prediction implements StringRepresentable {
        NONE, MATERIAL, PERSONAL_HISTORY;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    enum Comparison implements StringRepresentable {
        UNCHECKED, CONTRADICTED, VERIFIED;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
