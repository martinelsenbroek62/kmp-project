package com.nseindia.mc.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Temporary table to upload documents.
 */
@AllArgsConstructor
@Entity
@NoArgsConstructor
@Data
@Builder
@Table(name = "TBL_Document_Upload_KMP")
public class DocumentUpload extends AuditableEntity {
    @Id
    @Column(name = "id")
    private String docId;

    @Column(name = "app_id")
    private String applicationId;

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "folder_index")
    private long folderIndex;

    @Column(name = "doc_index")
    private long docIndex;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "doc_type")
    private String documentType;

    @Column(name = "extention")
    private String extension;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "file_size")
    private long fileSize;

    @Column(name = "sub_request_type")
    private String subRequestType;

    @Lob
    @Column(name = "file_data")
    private byte[] fileData;
}
