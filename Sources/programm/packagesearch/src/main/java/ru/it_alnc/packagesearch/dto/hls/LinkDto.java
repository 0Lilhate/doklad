/*
 * Copyright (c) 2019-2025 IT-Alliance, LLC. All rights reserved,
 * licensing is carried out on the basis of a license agreement
 * with the copyright holder IT-Alliance, LLC.
 * www.it-alnc.ru
 */

package ru.it_alnc.packagesearch.dto.hls;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LinkDto {
    @JsonProperty("Id")
    private String id;
    private String source;
    private List<LinkRef> refs;
    @JsonProperty("BOData")
    private String boData;

    @Getter
    @Setter
    public static class LinkRef {
        String name;
        String id;
    }
}

/*    {
      "Id": "50c8d344-8d1f-4a00-b709-a2532eb90d36",
      "refs": [
        {
          "name": "hub_ClientJurAddress",
          "id": "580b878a-1954-4b23-b312-5f45ca7acc00"
        },
        {
          "name": "hub_ClientJur",
          "id": "56032059-1086-4d2b-983e-5ca80538543d"
        }
      ],
      "Source": "",
      "CreatedOn": "2023-04-05T16:33:31.280+0300",
      "RemovedOn": null,
      "CreatedById": "",
      "CreatedByName": "",
      "BOData": "link_ClientJurAddress_ClientJur",
      "hashkey": "93b3207ee348511e0264f8dc0bbd73ab"
    },
*/