package com.api.webvote.tests.service;

import com.api.webvote.v1.enums.VotoEnum;
import com.api.webvote.v1.exception.BadRequestException;
import com.api.webvote.v1.model.Associate;
import com.api.webvote.v1.model.Schedule;
import com.api.webvote.v1.model.Vote;
import com.api.webvote.v1.repository.VoteRepository;
import com.api.webvote.v1.service.check.CheckExpiration;
import com.api.webvote.v1.service.check.CheckResponse;
import com.api.webvote.v1.service.check.CheckVotes;
import com.api.webvote.v1.service.vote.VoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class VoteServiceTests {

	@InjectMocks
	private VoteService voteService;

	@Mock
	private VoteRepository voteRepository;

	@BeforeEach
	public void initialize() {
		openMocks(this);
		when(voteRepository.save(voteMock)).thenReturn(voteMock);
	}

	List<Vote> votes = new ArrayList<Vote>();
	Associate associateMock = new Associate(1L, "Ralf Drehmer Wink", "000.000.000-00");

	Schedule scheduleMock = new Schedule(1L, "Schedule title", votes, 1, LocalDateTime.now(),
			LocalDateTime.now().plusMinutes(1));

	Vote voteMock = new Vote(1L, VotoEnum.SIM, associateMock, scheduleMock);

	@Test
	public void deveRetornarSucesso_aoCriarNovoVoto() throws Exception {
		ResponseEntity<Vote> response = voteService.save(voteMock);

		verify(voteRepository, times(1)).save(any(Vote.class));
		assertEquals(response, ResponseEntity.ok().build());
	}

	@Test
	public void deveRetornarSucesso_aoVerificarARespostaDoAssociado() throws Exception {
		assertDoesNotThrow(() -> CheckResponse.check(voteMock));
	}
	@Test
	public void deveRetornarFalha_aoVerificarARespostaDoAssociado() throws Exception {
		Vote voteMock = new Vote(1L, null, associateMock, scheduleMock);
		assertThrows(BadRequestException.class, () -> CheckResponse.check(voteMock));
	}
	@Test
	public void deveRetornarSucesso_aoVerificarSeAssociadoJaVotouNaPauta() throws Exception {
		assertDoesNotThrow(() -> CheckVotes.check(associateMock, scheduleMock));
	}

	@Test
	public void deveRetornarFalha_aoVerificarSeAssociadoJaVotouNaPauta() throws Exception {
		votes.add(voteMock);
		assertThrows(BadRequestException.class, () -> CheckVotes.check(associateMock, scheduleMock));
	}

	@Test
	public void deveRetornarFalha_aoVerificarSeAPautaExpirou() throws Exception {
		scheduleMock.setEnd(LocalDateTime.now().plusMinutes(-5));
		assertThrows(BadRequestException.class, () -> CheckExpiration.check(scheduleMock));
	}

	@Test
	public void deveRetornarSucesso_aoVerificarSeAPautaExpirou() throws Exception {
		assertDoesNotThrow(() -> CheckExpiration.check(scheduleMock));
	}

}
