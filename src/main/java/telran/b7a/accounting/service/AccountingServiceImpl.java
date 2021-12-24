package telran.b7a.accounting.service;

import java.time.LocalDate;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import telran.b7a.accounting.dao.AccountingMongoRepository;
import telran.b7a.accounting.dto.UpdateUserDto;
import telran.b7a.accounting.dto.UserRegisterDto;
import telran.b7a.accounting.dto.UserResponseDto;
import telran.b7a.accounting.dto.UserRolesDto;
import telran.b7a.accounting.dto.exceptions.UserAlreadyExistsException;
import telran.b7a.accounting.dto.exceptions.UserNotFoundException;
import telran.b7a.accounting.model.User;

@Service
public class AccountingServiceImpl implements UserAccountService {
	
	AccountingMongoRepository accRepository;
	ModelMapper modelMapper;
	PasswordEncoder passwordEncoder;
	
	@Autowired
	public AccountingServiceImpl(AccountingMongoRepository accRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
		this.accRepository = accRepository;
		this.modelMapper = modelMapper;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public UserResponseDto regiserNewUser(UserRegisterDto userInfo) {
		if (accRepository.existsById(userInfo.getLogin())) {
			throw new UserAlreadyExistsException();
		}
		User user = modelMapper.map(userInfo, User.class);
		String password = passwordEncoder.encode(userInfo.getPassword());
		user.setPassword(password);
		user.addRole("USER");
		user.setPasswordExpDate(LocalDate.now().plusMonths(1));
		accRepository.save(user);
		return modelMapper.map(user, UserResponseDto.class);
	}

	@Override
	public UserResponseDto getUser(String login) {
		User user = accRepository.findById(login).orElseThrow(() -> new UserNotFoundException());
		return modelMapper.map(user, UserResponseDto.class);
	}

	@Override
	public UserResponseDto deleteUser(String login) {
		User user = accRepository.findById(login).orElseThrow(() -> new UserNotFoundException());
		accRepository.deleteById(login);
		return modelMapper.map(user, UserResponseDto.class);
	}

	@Override
	public UserResponseDto editUser(UpdateUserDto newUserData, String login) {
		User user = accRepository.findById(login).orElseThrow(() -> new UserNotFoundException());
		user.setFirstName(newUserData.getFirstNameOptional().orElse(user.getFirstName()));
		user.setLastName(newUserData.getLastNameOptional().orElse(user.getLastName()));
		accRepository.save(user);
		return modelMapper.map(user, UserResponseDto.class);
	}

	@Override
	public UserRolesDto addRole(String login, String role) {
		User user = accRepository.findById(login).orElseThrow(() -> new UserNotFoundException());
		user.addRole(role.toUpperCase());
		accRepository.save(user);
		return modelMapper.map(user, UserRolesDto.class);
	}

	@Override
	public UserRolesDto deleteRole(String login, String role) {
		User user = accRepository.findById(login).orElseThrow(() -> new UserNotFoundException());
		user.deleteRole(role);
		accRepository.save(user);
		return modelMapper.map(user, UserRolesDto.class);
	}

	@Override
	public void changePassword(String login, String password) {
		User user = accRepository.findById(login).orElseThrow(() -> new UserNotFoundException());
		user.setPassword(passwordEncoder.encode(password));
		user.setPasswordExpDate(LocalDate.now().plusMonths(1));
		accRepository.save(user);
	}

}
