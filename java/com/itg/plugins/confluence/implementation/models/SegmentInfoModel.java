/*
 * Author: steve.killelay
 * Last Updated: 21/02/19 22:13
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
package com.itg.plugins.confluence.implementation.models;

import com.itg.plugins.confluence.implementation.abstrct.AbstractModels;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Steve.Killelay
 */
@XmlRootElement
public class SegmentInfoModel extends AbstractModels {

    @XmlElement(name = "id")
    public int id;
    @XmlElement(name = "server_name", required = true)
    public String serverName;
    @XmlElement(name = "database", required = true)
    public String database;
    @XmlElement(name = "schema", required = true)
    public String schema;
    @XmlElement(name = "segment_table", required = true)
    public String segmentTable;
    @XmlElement(name = "brand", required = true)
    public String brand;
    @XmlElement(name = "mailing_id", required = true)
    public int mailingId;
    @XmlElement(name = "session_key", required = true)
    public long sessionKey;

    private final Map<String, String> aliasMap;

    public SegmentInfoModel() {

        aliasMap = new HashMap<>();

        aliasMap.put("server_name", "Server Name");
        aliasMap.put("database", "Database");
        aliasMap.put("schema", "Schema");
        aliasMap.put("segment_table", "Segment Table");
        aliasMap.put("brand", "Brand");
        aliasMap.put("mailing_id", "MailingId");
    }

    @Override
    public String getFieldAlias(String fieldName) {

        return aliasMap.get(fieldName);
    }
}
