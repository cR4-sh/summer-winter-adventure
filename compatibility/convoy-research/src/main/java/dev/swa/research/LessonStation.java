package dev.swa.research;

import java.util.Locale;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

final class LessonStation extends Block {
    static final EnumProperty<Action> ACTION = EnumProperty.create("action", Action.class);

    LessonStation(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ACTION, Action.ENROLL));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTION);
    }

    enum Action implements StringRepresentable {
        ENROLL, CONTROL_GLOVE, HISTORICAL_GLOVE, PREDICT_MATERIAL, PREDICT_PERSONAL_HISTORY,
        COMPARISON_TOKEN, SHIP_BUCKLE;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
