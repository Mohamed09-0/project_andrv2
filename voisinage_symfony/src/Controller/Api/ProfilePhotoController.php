<?php

namespace App\Controller\Api;

use App\Entity\User;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Attribute\Route;

class ProfilePhotoController extends AbstractController
{
    #[Route(
        '/api/profile/photo',
        name: 'app_api_profile_photo',
        methods: ['POST']
    )]
    public function upload(
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
        $imageBase64 = $data['image_base64'] ?? null;

        if (!$userId || !is_numeric($userId) || !$imageBase64) {
            return $this->json([
                'success' => false,
                'message' => 'User ID and image are required'
            ], 400);
        }

        $user = $entityManager
            ->getRepository(User::class)
            ->find((int) $userId);

        if (!$user) {
            return $this->json([
                'success' => false,
                'message' => 'User not found'
            ], 404);
        }

        // Decode Base64 image
        $imageData = base64_decode($imageBase64, true);

        if ($imageData === false) {
            return $this->json([
                'success' => false,
                'message' => 'Invalid image encoding'
            ], 400);
        }

        // Check that the uploaded data is really an image
        $imageInfo = @getimagesizefromstring($imageData);

        if ($imageInfo === false) {
            return $this->json([
                'success' => false,
                'message' => 'The uploaded file is not a valid image'
            ], 400);
        }

        // Accept JPEG, PNG and WebP only
        $allowedTypes = [
            IMAGETYPE_JPEG => 'jpg',
            IMAGETYPE_PNG => 'png',
            IMAGETYPE_WEBP => 'webp'
        ];

        $imageType = $imageInfo[2];

        if (!isset($allowedTypes[$imageType])) {
            return $this->json([
                'success' => false,
                'message' => 'Only JPG, PNG and WebP images are accepted'
            ], 400);
        }

        // Limit image size to 5 MB
        if (strlen($imageData) > 5 * 1024 * 1024) {
            return $this->json([
                'success' => false,
                'message' => 'Image is too large (maximum 5 MB)'
            ], 400);
        }

        $extension = $allowedTypes[$imageType];

        // Create a unique filename
        $filename = bin2hex(random_bytes(16)) . '.' . $extension;

        // Save image in public/uploads/profiles
        $uploadDirectory = $this->getParameter('kernel.project_dir')
            . '/public/uploads/profiles';

        if (!is_dir($uploadDirectory)) {
            mkdir($uploadDirectory, 0755, true);
        }

        $filePath = $uploadDirectory . '/' . $filename;

        if (file_put_contents($filePath, $imageData) === false) {
            return $this->json([
                'success' => false,
                'message' => 'Could not save the image'
            ], 500);
        }

        // Save relative image path in the database
        $relativePath = '/uploads/profiles/' . $filename;

        $user->setPhoto($relativePath);

        $entityManager->flush();

        return $this->json([
            'success' => true,
            'message' => 'Profile photo uploaded successfully',
            'photo' => $relativePath
        ]);
    }
}