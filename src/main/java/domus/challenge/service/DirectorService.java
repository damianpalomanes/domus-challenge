package domus.challenge.service;

import java.util.List;

public interface DirectorService {
    List<String> getDirectorsByThreshold(int threshold);
}