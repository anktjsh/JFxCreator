/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.core;

import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author Aniket
 */
public class Resource extends Program {

    public Resource(Path p, List<String> cod, Project pro) {
        super(p, cod, pro);
    }

}
