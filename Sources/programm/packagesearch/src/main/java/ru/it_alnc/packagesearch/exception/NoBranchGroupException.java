/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.exception;

import org.springframework.core.NestedCheckedException;

public class NoBranchGroupException extends NestedCheckedException {
    public NoBranchGroupException(String message) {
        super(message);
    }

    public NoBranchGroupException(String message, Throwable cause) {
        super(message, cause);
    }

}
