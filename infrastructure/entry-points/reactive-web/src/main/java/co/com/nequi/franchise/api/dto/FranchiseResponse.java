package co.com.nequi.franchise.api.dto;

import java.util.List;

public record FranchiseResponse(String id, String name, List<BranchResponse> branches) {
}
