/*
 * Author: steve.killelay
 * Last Updated: 22/02/19 11:43
 *
 * Copyright {c} 2019, ITG
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,  this list of conditions and the following disclaimer in the documentation  and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */
package com.itg.plugins.confluence.interfaces;

import com.itg.plugins.confluence.implementation.models.SegmentInfoModel;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Steve.Killelay
 */
public interface ISegmentInfoProvider {

    List<Map<String, Object>> getSegment(String sessionId, int id) throws SQLException;

    List<Map<String, Object>> getSegmentSession(String sessionKey) throws SQLException;

    Map<String, Object> createSegment(SegmentInfoModel segment) throws SQLException;

    Map<String, Object> updateSegment(SegmentInfoModel segment) throws SQLException;

    void deleteSegment(int id) throws SQLException;

    void deleteSegmentSession(String sessionKey) throws SQLException;
}
