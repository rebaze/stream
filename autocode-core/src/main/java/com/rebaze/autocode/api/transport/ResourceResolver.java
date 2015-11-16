package com.rebaze.autocode.api.transport;

import com.rebaze.commons.tree.Tree;

/**
 * @author Toni Menzel< (toni.menzel@rebaze.com)
 */
public interface ResourceResolver<T>
{
    Tree resolve(T query);
}
