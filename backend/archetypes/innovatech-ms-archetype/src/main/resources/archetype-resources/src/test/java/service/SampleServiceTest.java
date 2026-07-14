package ${package}.service;

import ${package}.dto.CreateSampleRequest;
import ${package}.dto.SampleDto;
import ${package}.dto.UpdateSampleRequest;
import ${package}.entity.Sample;
import ${package}.exception.NotFoundException;
import ${package}.repository.SampleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SampleServiceTest {

    @Mock
    SampleRepository repo;

    @InjectMocks
    SampleService service;

    private Sample sample(Long id) {
        return new Sample(id, "demo", "a description", true);
    }

    @Test
    void create_persistsAndReturnsDto() {
        when(repo.save(any(Sample.class))).thenReturn(sample(1L));

        SampleDto dto = service.create(new CreateSampleRequest("demo", "a description"));

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("demo");
        assertThat(dto.active()).isTrue();
    }

    @Test
    void get_returnsDto_whenFound() {
        when(repo.findById(1L)).thenReturn(Optional.of(sample(1L)));

        assertThat(service.get(1L).description()).isEqualTo("a description");
    }

    @Test
    void get_throws_whenMissing() {
        when(repo.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(9L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void list_mapsAll() {
        when(repo.findAll()).thenReturn(List.of(sample(1L), sample(2L)));

        assertThat(service.list()).hasSize(2);
    }

    @Test
    void update_appliesChanges_whenFound() {
        when(repo.findById(1L)).thenReturn(Optional.of(sample(1L)));
        when(repo.save(any(Sample.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<SampleDto> updated = service.update(1L, new UpdateSampleRequest("changed", false));

        assertThat(updated).isPresent();
        assertThat(updated.get().description()).isEqualTo("changed");
        assertThat(updated.get().active()).isFalse();
    }

    @Test
    void delete_returnsFalse_whenMissing() {
        when(repo.existsById(5L)).thenReturn(false);

        assertThat(service.delete(5L)).isFalse();
    }
}
