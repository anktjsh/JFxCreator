/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.process;

import java.util.EventListener;

/**
 *
 * @author Aniket
 */
public interface ProcessListener extends EventListener {

    void processFinished(Process process, int exitValue);
}
