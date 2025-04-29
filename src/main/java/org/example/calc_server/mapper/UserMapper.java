package org.example.calc_server.mapper;


import org.example.calc_server.dto.UserCreateDTO;
import org.example.calc_server.dto.UserDTO;
import org.example.calc_server.model.User;
import org.mapstruct.*;

@Mapper(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class UserMapper {
    @Mapping(target = "password", source = "encryptedPassword")
    public abstract User map(UserCreateDTO dto);

    public abstract UserDTO map(User model);
}
