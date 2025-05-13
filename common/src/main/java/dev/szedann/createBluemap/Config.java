package dev.szedann.createBluemap;

import me.fzzyhmstrs.fzzy_config.api.SaveType;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;
import org.jetbrains.annotations.NotNull;

public class Config extends me.fzzyhmstrs.fzzy_config.config.Config {
    public Config() {
        super(CreateBluemap.asResource("config"));
    }
    public ValidatedInt interval = new ValidatedInt(5, 30, 1);
    public ValidatedBoolean renderTracks = new ValidatedBoolean(false);
    public ValidatedBoolean renderCarriages = new ValidatedBoolean(true);
    public ValidatedBoolean renderTrains = new ValidatedBoolean(false);

    @Override
    public @NotNull SaveType saveType() {
        return SaveType.SEPARATE;
    }
}
