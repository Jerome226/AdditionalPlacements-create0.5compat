package com.firemerald.dvsas.client.models;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public record ModelKey(BlockState state, Direction side, RenderType renderType) {}