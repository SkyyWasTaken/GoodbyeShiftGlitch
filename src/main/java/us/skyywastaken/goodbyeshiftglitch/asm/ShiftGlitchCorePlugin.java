package us.skyywastaken.goodbyeshiftglitch.asm;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;


@IFMLLoadingPlugin.TransformerExclusions({"us.skyywastaken.goodbyeshiftglitch.asm"})
@IFMLLoadingPlugin.MCVersion("1.8.9")
@IFMLLoadingPlugin.SortingIndex(Integer.MIN_VALUE)
public class ShiftGlitchCorePlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return new String[]{"us.skyywastaken.goodbyeshiftglitch.asm.EntityTransformer"};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
