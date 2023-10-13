package com.leonardozw.todolistrocketseat.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.leonardozw.todolistrocketseat.user.UserRepository;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter{

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        var servletPath = request.getServletPath();

        if(servletPath.startsWith("/tasks")){
            var authorization = request.getHeader("Authorization");
            var encodedPass = authorization.substring("Basic".length()).trim();

            byte[] decodedBytePass = Base64.getDecoder().decode(encodedPass);

            var decodedPassString = new String(decodedBytePass);

            String credentials[] = decodedPassString.split(":");
            String username = credentials[0];
            String password = credentials[1];

            var user = this.userRepository.findByUsername(username);
            if(user == null){
                response.sendError(401);
            }else{
                var passVerified = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
                if(passVerified.verified){
                    request.setAttribute("userId", user.getId());
                    filterChain.doFilter(request, response);
                }else{
                    response.sendError(401);
                }
            }
        }else{
            filterChain.doFilter(request, response);
        }
    }

}
