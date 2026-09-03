package co.com.nequi.franchise.model.franchise;

import co.com.nequi.franchise.model.franchise.branch.Branch;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.With;

import java.util.List;

@Value
@Builder(toBuilder = true)
public class Franchise {

    @With
    String id;

    @With
    String name;

    @With
    @Singular
    List<Branch> branches;
}
