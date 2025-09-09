package dev.szedann.createBluemap;

import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;

public class Config extends me.fzzyhmstrs.fzzy_config.config.Config {
    public Config() {
        super(CreateBluemap.asResource("config"));
    }
    
    // Update intervals (in seconds)
    public ValidatedInt trainUpdateInterval = new ValidatedInt(1, 10, 1); // Fast updates for trains
    public ValidatedInt trackUpdateInterval = new ValidatedInt(30, 300, 10); // Slow updates for tracks
    
    // Rendering toggles
    public ValidatedBoolean renderTracks = new ValidatedBoolean(true);
    public ValidatedBoolean renderCarriages = new ValidatedBoolean(false); // Default off for performance
    public ValidatedBoolean renderTrains = new ValidatedBoolean(true);
    
    // Performance settings
    public ValidatedBoolean onlyRenderMovingTrains = new ValidatedBoolean(false); // Show all trains by default
    public ValidatedBoolean enableMovementDetection = new ValidatedBoolean(true); // Skip unchanged positions
    
    // Movement detection threshold (in blocks)
    public ValidatedInt movementThreshold = new ValidatedInt(1, 10, 1); // 0.1 to 1.0 blocks
    
    // Visibility settings
    public ValidatedBoolean trainsVisibleByDefault = new ValidatedBoolean(true); // Fix hidden trains issue
    public ValidatedBoolean carriagesVisibleByDefault = new ValidatedBoolean(false);
    public ValidatedBoolean tracksVisibleByDefault = new ValidatedBoolean(false);
    
    // Icon settings
    public ValidatedInt trainIconSize = new ValidatedInt(48, 48, 4); // Icon size in pixels (default 48x48)
    
    // Marker visibility settings
    public ValidatedInt markerMaxDistance = new ValidatedInt(10000, 50000, 1000); // Max distance for marker visibility
    
    // Legacy compatibility (for existing configs)
    public ValidatedInt interval = new ValidatedInt(5, 30, 1); // Deprecated, use trainUpdateInterval
}
