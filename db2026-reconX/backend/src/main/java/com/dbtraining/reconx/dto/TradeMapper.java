package com.dbtraining.reconx.dto;

import org.mapstruct.*;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TradeMapper {

    @Mapping(source = "counterparty.id",       target = "counterpartyId")
    @Mapping(source = "counterparty.name",     target = "counterpartyName")
    @Mapping(source = "instrument.id",         target = "instrumentId")
    @Mapping(source = "instrument.symbol",     target = "instrumentSymbol")
    @Mapping(source = "status",                target = "status",
             qualifiedByName = "statusToString")
    TradeResponse toResponse(Trade trade);

    @Mapping(target = "id",            ignore = true)
    @Mapping(target = "counterparty",  ignore = true)   // wired by service from id
    @Mapping(target = "instrument",    ignore = true)
    @Mapping(target = "status",        ignore = true)   // defaulted to PENDING
    @Mapping(target = "createdAt",     ignore = true)
    @Mapping(target = "modifiedAt",    ignore = true)
    Trade toEntity(TradeRequest req);

    @Named("statusToString")
    static String statusToString(Enum<?> status) {
        return status == null ? null : status.name();
    }
}