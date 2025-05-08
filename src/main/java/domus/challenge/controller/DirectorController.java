package domus.challenge.controller;

import java.util.List;
import java.util.Map;

public interface DirectorController {
    Map<String, List<String>> getDirectors(int threshold);
}