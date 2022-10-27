package com.firemerald.additionalplacements.block;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

public class VerticalWeatheringSlabBlock<T extends SlabBlock & WeatheringCopper> extends VerticalSlabBlock implements WeatheringCopper
{
	public VerticalWeatheringSlabBlock(T slab)
	{
		super(slab);
	}
	
	@Override
	public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom)
	{
		this.onRandomTick(pState, pLevel, pPos, pRandom);
	}

	@Override
	public WeatherState getAge()
	{
		return ((WeatheringCopper) this.parentBlock).getAge();
	}
}