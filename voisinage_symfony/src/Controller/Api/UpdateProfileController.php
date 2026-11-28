<?php

namespace App\Controller\Api;

use App\Entity\User;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Attribute\Route;

class UpdateProfileController extends AbstractController
{
    #[Route('/api/profile/update', name: 'app_api_profile_update', methods: ['POST'])]
    public function update(
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

        $userId = $data['user_id'] ?? null;

        if (!$userId || !is_numeric($userId)) {
            return $this->json([
                'success' => false,
                'message' => 'User ID is required'
            ], 400);
        }

        $user = $entityManager->getRepository(User::class)
            ->find($userId);

        if (!$user) {
            return $this->json([
                'success' => false,
                'message' => 'User not found'
            ], 404);
        }

        $nom = trim($data['nom'] ?? '');
        $prenom = trim($data['prenom'] ?? '');
        $email = trim($data['email'] ?? '');
        $login = trim($data['login'] ?? '');
        $telephone = trim($data['telephone'] ?? '');

        if ($nom === '' || $prenom === '' ||
            $email === '' || $login === '') {
            return $this->json([
                'success' => false,
                'message' => 'Please complete all required fields'
            ], 400);
        }

        if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
            return $this->json([
                'success' => false,
                'message' => 'Invalid email address'
            ], 400);
        }

        // Check whether another user already uses this email
        $existingEmail = $entityManager->getRepository(User::class)
            ->findOneBy(['email' => $email]);

        if ($existingEmail && $existingEmail->getId() !== $user->getId()) {
            return $this->json([
                'success' => false,
                'message' => 'Email already exists'
            ], 409);
        }

        // Check whether another user already uses this username
        $existingLogin = $entityManager->getRepository(User::class)
            ->findOneBy(['login' => $login]);

        if ($existingLogin && $existingLogin->getId() !== $user->getId()) {
            return $this->json([
                'success' => false,
                'message' => 'Username already exists'
            ], 409);
        }

        // Update user information
        $user->setNom($nom);
        $user->setPrenom($prenom);
        $user->setEmail($email);
        $user->setLogin($login);
        $user->setTelephone($telephone);

        // Save changes in MySQL
        $entityManager->flush();

        return $this->json([
            'success' => true,
            'message' => 'Profile updated successfully',
            'user' => [
                'id' => $user->getId(),
                'nom' => $user->getNom(),
                'prenom' => $user->getPrenom(),
                'email' => $user->getEmail(),
                'login' => $user->getLogin(),
                'telephone' => $user->getTelephone()
            ]
        ]);
    }
}
