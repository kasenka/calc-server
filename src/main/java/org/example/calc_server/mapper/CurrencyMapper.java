package org.example.calc_server.mapper;

import org.example.calc_server.dto.CurrencyCreateDTO;
import org.example.calc_server.dto.UserCreateDTO;
import org.example.calc_server.dto.UserDTO;
import org.example.calc_server.model.CurrencyRate;
import org.example.calc_server.model.User;
import org.mapstruct.*;

@Mapper(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class CurrencyMapper {
//    @Mapping(target = "encryptedPassword", source = "password")
    public abstract CurrencyRate map(CurrencyCreateDTO dto);

//    public abstract UserDTO map(User model);
}