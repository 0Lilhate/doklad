/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.exception;

public class CheckConditionException extends RuntimeException {
    public CheckConditionException() {
        super();
    }

    public CheckConditionException(String s) {
        super(s);
    }

    public CheckConditionException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
