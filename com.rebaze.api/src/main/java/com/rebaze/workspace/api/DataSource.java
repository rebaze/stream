package com.rebaze.workspace.api;

import java.net.URI;

/**
 * Provides physical access to the real underlying resource. Never expose it beyond {@link WorkspaceAdmin}
 * 
 * @author tonit
 *
 */
 public interface DataSource {
	URI uri();
}
