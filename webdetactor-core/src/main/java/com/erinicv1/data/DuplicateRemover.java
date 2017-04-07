package com.erinicv1.data;

/**
 * Created by Administrator on 2017/4/6 0006.
 */
public interface DuplicateRemover<ID> {

    boolean isDuplicate(ID id);
}
