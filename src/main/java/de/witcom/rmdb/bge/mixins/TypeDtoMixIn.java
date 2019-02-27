package de.witcom.rmdb.bge.mixins;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fntsoftware.businessgateway.internal.doc.dto.ComplexTypeDto;
import com.fntsoftware.businessgateway.internal.doc.dto.ListTypeDto;
import com.fntsoftware.businessgateway.internal.doc.dto.SimpleTypeDto;
import com.fntsoftware.businessgateway.internal.doc.dto.TypeReferenceDto;


@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="@type")
@JsonSubTypes({@Type(value=SimpleTypeDto.class, name="simple"), @Type(value=ListTypeDto.class, name="list"), @Type(value=ComplexTypeDto.class, name="complex"), @Type(value=TypeReferenceDto.class, name="reference")})
public interface TypeDtoMixIn {

}
