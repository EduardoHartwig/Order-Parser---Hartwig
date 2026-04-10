package service;

import domain.User;

import java.util.List;

public interface NormalizationService {

    List<User> normalizeOrders(List<String> lines);
}
