package io.github.vishalmysore.util;

import java.util.*;

/**
 * Utility interface for creating A2UI display components
 * Extends the parent A2UIDisplay from a2ajava library with additional helper methods
 * 
 * Note: The a2ajava framework's buildA2UIMessageWithData() returns a Map combining
 * surfaceUpdate, dataModelUpdate, and beginRendering. While A2UI v0.8 protocol specifies
 * these should be separate JSONL messages, the framework handles the serialization.
 */
public interface A2UIDisplay extends io.github.vishalmysore.a2ui.A2UIDisplay {
    
    // All helper methods are inherited from parent io.github.vishalmysore.a2ui.A2UIDisplay
    // including:
    // - createRootColumn()
    // - createTextComponent()
    // - createTextFieldComponent()
    // - createButtonComponent()
    // - buildA2UIMessageWithData()
    // - isUICallback()
    
}

