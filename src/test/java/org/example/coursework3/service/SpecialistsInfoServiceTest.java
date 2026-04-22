package org.example.coursework3.service;

import org.example.coursework3.entity.Expertise;
import org.example.coursework3.entity.Specialist;
import org.example.coursework3.entity.SpecialistStatus;
import org.example.coursework3.repository.SpecialistsRepository;
import org.example.coursework3.vo.SpecialistsDetailVo;
import org.example.coursework3.vo.SpecialistsPageVo;
import org.example.coursework3.vo.SpecialistsVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpecialistsInfoServiceTest {

    @Mock
    private SpecialistsRepository specialistsRepository;

    @InjectMocks
    private SpecialistsInfoService specialistsInfoService;

    @Test
    void getSpecialistDetail_returnsDetailForValidId() {
        Expertise expertise = new Expertise();
        expertise.setId("e1");
        expertise.setName("Mental Health");

        Specialist specialist = new Specialist();
        specialist.setUserId("u3");
        specialist.setName("Dr.Smith");
        specialist.setBio("Psychologist with 10 years experience");
        specialist.setExpertises(List.of(expertise));
        specialist.setPrice(BigDecimal.valueOf(100));

        when(specialistsRepository.findById(eq("u3")))
                .thenReturn(Optional.of(specialist));

        SpecialistsDetailVo detail = specialistsInfoService.getSpecialistDetail("u3");
        assertEquals("u3", detail.getId());
        assertEquals("Dr.Smith", detail.getName());
        assertEquals("Psychologist with 10 years experience", detail.getBio());
        assertEquals(1, detail.getExpertise().size());
        assertEquals("e1", detail.getExpertise().get(0).getId());
        assertEquals("Mental Health", detail.getExpertise().get(0).getName());
        assertEquals(BigDecimal.valueOf(100), detail.getPrice());
    }

    @Test
    void getSpecialistDetail_throwsWhenSpecialistNotFound() {
        when(specialistsRepository.findById(eq("invalid-id")))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> specialistsInfoService.getSpecialistDetail("invalid-id"));
        assertEquals("specialist not found: invalid-id", ex.getMessage());
    }

    @Test
    void getSpecialists_returnsPagedResultWithoutExpertiseFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Expertise expertise = new Expertise();
        expertise.setId("e1");

        Specialist specialist = new Specialist();
        specialist.setUserId("u3");
        specialist.setName("Dr.Smith");
        specialist.setStatus(SpecialistStatus.Active);
        specialist.setExpertises(List.of(expertise));
        specialist.setPrice(BigDecimal.valueOf(100));

        Page<Specialist> specialistPage = new PageImpl<>(List.of(specialist), pageable, 1);

        when(specialistsRepository.findAll(eq(pageable)))
                .thenReturn(specialistPage);

        SpecialistsPageVo pageVo = specialistsInfoService.getSpecialists(null, 1, 10);
        assertEquals(1, pageVo.getTotal());
        assertEquals(1, pageVo.getPage());
        assertEquals(10, pageVo.getPageSize());
        assertEquals(1, pageVo.getItems().size());

        SpecialistsVo vo = pageVo.getItems().get(0);
        assertEquals("u3", vo.getId());
        assertEquals("Dr.Smith", vo.getName());
        assertEquals(SpecialistStatus.Active, vo.getStatus());
        assertEquals(List.of("e1"), vo.getExpertiseIds());
        assertEquals(BigDecimal.valueOf(100), vo.getPrice());
    }

    @Test
    void getSpecialists_returnsPagedResultWithExpertiseFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Expertise expertise = new Expertise();
        expertise.setId("e1");

        Specialist specialist = new Specialist();
        specialist.setUserId("u3");
        specialist.setName("Dr.Smith");
        specialist.setStatus(SpecialistStatus.Active);
        specialist.setExpertises(List.of(expertise));
        specialist.setPrice(BigDecimal.valueOf(100));

        Page<Specialist> specialistPage = new PageImpl<>(List.of(specialist), pageable, 1);

        when(specialistsRepository.findDistinctByExpertises_Id(eq("e1"), eq(pageable)))
                .thenReturn(specialistPage);

        SpecialistsPageVo pageVo = specialistsInfoService.getSpecialists("e1", 1, 10);
        assertEquals(1, pageVo.getTotal());
        assertEquals("u3", pageVo.getItems().get(0).getId());
        assertEquals(List.of("e1"), pageVo.getItems().get(0).getExpertiseIds());
    }

    @Test
    void getSpecialists_usesFindAllWhenExpertiseIdIsBlank() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Specialist> specialistPage = new PageImpl<>(List.of(), pageable, 0);

        when(specialistsRepository.findAll(eq(pageable)))
                .thenReturn(specialistPage);

        SpecialistsPageVo pageVo = specialistsInfoService.getSpecialists("   ", 1, 10);

        assertEquals(0, pageVo.getTotal());
        assertTrue(pageVo.getItems().isEmpty());
        verify(specialistsRepository).findAll(eq(pageable));
    }

    @Test
    void getSpecialists_adjustsInvalidPageAndPageSize() {
        Pageable pageable = PageRequest.of(0, 1);
        Specialist specialist = new Specialist();
        specialist.setUserId("u3");
        Page<Specialist> specialistPage = new PageImpl<>(List.of(specialist), pageable, 1);

        when(specialistsRepository.findAll(eq(pageable)))
                .thenReturn(specialistPage);

        // 传入无效的page(0)和pageSize(0)，验证自动修正
        SpecialistsPageVo pageVo = specialistsInfoService.getSpecialists(null, 0, 0);
        assertEquals(1, pageVo.getPage());
        assertEquals(1, pageVo.getPageSize());
    }

    @Test
    void getSpecialistDetail_returnsEmptyExpertiseListWhenNoExpertise() {
        Specialist specialist = new Specialist();
        specialist.setUserId("u4");
        specialist.setName("Dr. Lee");
        specialist.setBio("Career consultant and coach");
        specialist.setExpertises(List.of());
        specialist.setPrice(BigDecimal.valueOf(80));

        when(specialistsRepository.findById(eq("u4")))
                .thenReturn(Optional.of(specialist));

        SpecialistsDetailVo detail = specialistsInfoService.getSpecialistDetail("u4");
        assertEquals("u4", detail.getId());
        assertNotNull(detail.getExpertise());
        assertEquals(0, detail.getExpertise().size());
    }
}
