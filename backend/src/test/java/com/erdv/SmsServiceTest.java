package com.erdv;

import com.erdv.service.SmsService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SmsServiceTest {

    @Test
    void normalizePhoneFrenchMobile() {
        assertEquals("+33612345678", SmsService.normalizePhone("06 12 34 56 78"));
    }

    @Test
    void normalizePhoneAlreadyE164() {
        assertEquals("+33612345678", SmsService.normalizePhone("+33612345678"));
    }

    @Test
    void normalizePhoneInvalidReturnsNull() {
        assertNull(SmsService.normalizePhone(""));
        assertNull(SmsService.normalizePhone("abc"));
    }
}
