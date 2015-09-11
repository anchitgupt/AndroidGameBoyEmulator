package com.github.pedrovgs.androidgameboyemulator.core;

import com.github.pedrovgs.androidgameboyemulator.core.gameloader.FakeGameReader;
import com.github.pedrovgs.androidgameboyemulator.core.gameloader.GameLoader;
import com.github.pedrovgs.androidgameboyemulator.core.gpu.GPU;
import com.github.pedrovgs.androidgameboyemulator.core.mmu.MMU;
import com.github.pedrovgs.androidgameboyemulator.core.processor.GBZ80;
import java.io.IOException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class GameBoyBIOSTest {

  private static final int INITIALIZE_LCD_STAGE_TICKS = 26823;
  private static final int STACK_POINTER_INITIAL_VALUE = 0xFFFE;
  private static final int FIRST_STAGE_FINISH_PROGRAM_COUNTER = 8;
  private static final int INITIALIZE_AUDIO_STAGE_TICKS = 11;

  private GBZ80 z80;
  private MMU mmu;

  @Test public void shouldInitializeStackPointerToTheDefaultValueInTheFirstTick()
      throws IOException {
    GameBoy gameBoy = givenAGameBoy();

    tickGameBoy(gameBoy, 1);

    assertEquals(STACK_POINTER_INITIAL_VALUE, z80.getStackPointer());
  }

  @Test public void shouldInitializeVideoMemoryWithZero() throws IOException {
    GameBoy gameBoy = givenAGameBoy();

    tickUntilFirstBiosStageFinished(gameBoy);

    assertVideoMemoryIsInitializedToZero();
  }

  @Test public void shouldFinishFirstStageWithTheNExtStageProgramCounter() throws IOException {
    GameBoy gameBoy = givenAGameBoy();

    tickUntilFirstBiosStageFinished(gameBoy);

    assertEquals(FIRST_STAGE_FINISH_PROGRAM_COUNTER, z80.getProgramCounter());
  }

  @Test public void shouldNotFinishBIOSExecutionAtTheEndOfTheFirstStage() throws IOException {
    GameBoy gameBoy = givenAGameBoy();

    tickUntilFirstBiosStageFinished(gameBoy);

    assertFalse(mmu.isSystemReady());
  }

  @Test public void shouldInitializeAudioDevice() throws IOException {
    GameBoy gameBoy = givenAGameBoy();

    tickUntilSecondBiosStageFinished(gameBoy);

    //assertEquals(65315, z80.get16BitRegisterValue(Register.HL));
    //assertEquals(10, z80.get8BitRegisterValue(Register.C) & 0xFF);
  }

  private GameBoy givenAGameBoy() {
    z80 = new GBZ80();
    mmu = new MMU();
    GPU gpu = new GPU(mmu);
    GameLoader gameLoader = new GameLoader(new FakeGameReader());
    return new GameBoy(z80, mmu, gpu, gameLoader);
  }

  private void tickGameBoy(GameBoy gameBoy, int numberOfTicks) throws IOException {
    for (int i = 0; i < numberOfTicks; i++) {
      gameBoy.tick();
    }
  }

  private void tickUntilFirstBiosStageFinished(GameBoy gameBoy) throws IOException {
    tickGameBoy(gameBoy, INITIALIZE_LCD_STAGE_TICKS);
  }

  private void tickUntilSecondBiosStageFinished(GameBoy gameBoy) throws IOException {
    tickUntilFirstBiosStageFinished(gameBoy);
    tickGameBoy(gameBoy, INITIALIZE_AUDIO_STAGE_TICKS);
  }

  private void assertVideoMemoryIsInitializedToZero() {
    for (int address = 0x8000; address < 0x9FFF; address++) {
      byte value = mmu.readByte(address);
      assertEquals("Address = " + address + " not initialized to 0", 0, value);
    }
  }
}