<?php

namespace App\Controller\Api;

use App\Entity\User;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Attribute\Route;

class LoginController extends AbstractController
{
    #[Route('/api/login', name: 'app_api_login', methods: ['POST'])]
    public function login(
        Request $request,
        EntityManagerInterface $entityManager
    ): JsonResponse {

        $data = json_decode($request->getContent(), true);

        if (!is_array($data)) {
            return $this->json([
                'success' => false,
                'message' => 'Invalid JSON data'
            ], 400);
        }

        $login = trim($data['login'] ?? '');
        $password = $data['password'] ?? '';

        if ($login === '' || $password === '') {
            return $this->json([
                'success' => false,
                'message' => 'Login and password are required'
            ], 400);
        }

        // Find user by login
        $user = $entityManager
            ->getRepository(User::class)
            ->findOneBy(['login' => $login]);

        if (!$user || !password_verify(
            $password,
            $user->getMotDePasseHash()
        )) {
            return $this->json([
                'success' => false,
                'message' => 'Incorrect login or password'
            ], 401);
        }

        // Retrieve the user's location
        $location = $user->getLocalisation();

        $ville = $location ? $location->getVille() : null;
        $quartier = $location ? $location->getQuartier() : null;

        return $this->json([
            'success' => true,
            'message' => 'Login successful',

            'user' => [
                'id' => $user->getId(),
                'nom' => $user->getNom(),
                'prenom' => $user->getPrenom(),
                'email' => $user->getEmail(),
                'login' => $user->getLogin(),
                'telephone' => $user->getTelephone(),
                'photo' => $user->getPhoto(),
                'role' => $user->getRole(),

                // Location information
                'ville' => $ville,
                'quartier' => $quartier
            ]
        ]);
    }
}
